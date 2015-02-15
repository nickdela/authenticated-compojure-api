(ns authenticated-compojure-api.route-functions.user
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.sign.generic :as bs]
            [buddy.hashers.bcrypt :as hasher]
            [environ.core :refer [env]]
            [ring.util.http-response :as respond]
            [postal.core :refer [send-message]]))

(defn auth-credentials-response [request]
  (let [user    (:identity request)
        token   (bs/dumps user auth-key)
        refresh (:refresh_token user)]
    (respond/ok {:username (:username user) :token token :refresh_token refresh})))

(defn gen-new-token-response [refresh_token]
  (let [user (query/get-registered-user-by-reset-token {:refresh_token refresh_token})]
    (if (empty? user)
      (respond/bad-request {:error "Bad Request"})
      (respond/ok          {:token (bs/dumps user auth-key)}))))

(defn create-new-user [email username password]
  (let [refresh-token   (str (java.util.UUID/randomUUID))
        hashed-password (hasher/make-password password)
        new-user        (query/insert-registered-user<! {:email         email
                                              :username      username
                                              :password      hashed-password
                                              :refresh_token refresh-token})
        permission      (query/insert-permission-for-user<! {:userid     (:id new-user)
                                                             :permission "basic"})]
    (respond/created {:username (str (:username new-user))})))

(defn create-user-response [email username password]
  (let [username-query   (query/get-registered-user-by-username {:username username})
        email-query      (query/get-registered-user-by-email {:email email})
        email-exists?    (not-empty email-query)
        username-exists? (not-empty username-query)]
    (cond
      (and username-exists? email-exists?) (respond/conflict {:error "Username and Email already exist"})
      username-exists?                     (respond/conflict {:error "Username already exists"})
      email-exists?                        (respond/conflict {:error "Email already exists"})
      :else                                (create-new-user email username password))))

(defn delete-user [id]
  (let [deleted-user (query/delete-registered-user! {:id id})]
    (if (not= 0 deleted-user)
      (respond/ok        {:message (format "User id %d successfully removed" id)})
      (respond/not-found {:error "Userid does not exist"}))))

(defn delete-user-response [request id]
  (let [auth          (get-in request [:identity :permissions])
        deleting-self (= id (get-in request [:identity :id]))]
    (if (or (.contains auth "admin") deleting-self)
      (delete-user id)
      (respond/unauthorized {:error "Not authorized"}))))

(defn modify-user [current-user-info username password email]
  (let [new-email     (if (empty? email)    (str (:email current-user-info)) email)
        new-username  (if (empty? username) (str (:username current-user-info)) username)
        new-password  (if (empty? password) (:password current-user-info) (hasher/make-password password))
        new-user-info (query/update-registered-user<! {:id (:id current-user-info)
                                            :email new-email
                                            :username new-username
                                            :password new-password
                                            :refresh_token (:refresh_token current-user-info)})]
    (respond/ok {:id (:id current-user-info) :email new-email :username new-username})))

(defn modify-user-response [request id username password email]
  (let [auth              (get-in request [:identity :permissions])
        current-user-info (first (query/get-registered-user-by-id {:id id}))
        admin?            (.contains auth "admin")
        modify?           (and admin? (not-empty current-user-info))]
    (cond
      modify?                    (modify-user current-user-info username password email)
      (not admin?)               (respond/unauthorized {:error "Not authorized"})
      (empty? current-user-info) (respond/not-found {:error "Userid does not exist"}))))

(defn delete-user-permission [id permission]
  (let [deleted-permission (query/delete-user-permission! {:userid id :permission permission})]
    (if (not= 0 deleted-permission)
      (respond/ok        {:message (format "Permission '%s' for user %d successfully removed" permission id)})
      (respond/not-found {:error (format "User %s does not have %s permission" id)}))))

(defn delete-user-permission-response [request id permission]
  (let [auth (get-in request [:identity :permissions])]
    (if (.contains auth "admin")
      (delete-user-permission id permission)
      (respond/unauthorized {:error "Not authorized"}))))

(defn add-user-permission [id permission]
  (let [added-permission (try
                           (query/insert-permission-for-user<! {:userid id :permission permission})
                           (catch Exception e 0))]
    (if (not= 0 added-permission)
      (respond/ok        {:message (format "Permission '%s' for user %d successfully added" permission id)})
      (respond/not-found {:error (format "Permission '%s' does not exist" permission)}))))

(defn add-user-permission-response [request id permission]
  (let [auth (get-in request [:identity :permissions])]
    (if (.contains auth "admin")
      (add-user-permission id permission)
      (respond/unauthorized {:error "Not authorized"}))))

(defn add-response-link-to-plain-body [body response-link]
  (str body "\n\n" response-link))

(defn add-response-link-to-html-body [body response-link]
  (let [body-less-closing-tags (clojure.string/replace body #"</body></html>" "")]
    (str body-less-closing-tags "<br><p>" response-link "</p></body></html>")))

(defn send-reset-email [to-email from-email subject html-body plain-body]
  (send-message {:host "smtp.mandrillapp.com"
                 :user (env :user-email)
                 :port 587
                 :pass (env :user-pass-key)}
                {:from from-email
                 :to to-email
                 :subject subject
                 :body [:alternative
                        {:type "text/plain"
                         :content plain-body}
                        {:type "text/html"
                         :content html-body}]}))

(defn process-password-reset-request [user from-email subject email-body-plain email-body-html response-base-link]
  (let [reset-key     (str (java.util.UUID/randomUUID))
        the-insert    (query/insert-password-reset-key-with-default-valid-until<! {:reset_key reset-key :user_id (:id user)})
        response-link (str response-base-link "/" (:reset_key the-insert))
        body-plain    (add-response-link-to-plain-body email-body-plain response-link)
        body-html     (add-response-link-to-html-body email-body-html response-link)]
    (send-reset-email (str (:email user)) from-email subject body-html body-plain)
    (respond/ok {:message (str "Reset email successfully sent to " (str (:email user)))})))

(defn request-password-reset-response [user-email from-email subject email-body-plain email-body-html response-base-link]
  (let [user (first (query/get-registered-user-by-email {:email user-email}))]
    (if (empty? user)
      (respond/not-found {:error (str "No user exists with the email " user-email)})
      (process-password-reset-request user from-email subject email-body-plain email-body-html response-base-link))))

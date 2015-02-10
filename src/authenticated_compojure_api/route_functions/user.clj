(ns authenticated-compojure-api.route-functions.user
  (:require [authenticated-compojure-api.auth-resources.auth-key :refer [auth-key]]
            [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.sign.generic :as bs]
            [buddy.hashers.bcrypt :as hasher]
            [ring.util.http-response :as respond-with]))

(defn auth-credentials-response [request]
  (respond-with/ok (let [user    (:identity request)
                         token   (bs/dumps user auth-key)
                         refresh (:refresh_token user)]
        {:username (:username user) :token token :refresh_token refresh})))

(defn gen-new-token-response [refresh_token]
  (let [user (query/get-user-by-reset-token {:refresh_token refresh_token})]
    (if (empty? user)
      (respond-with/bad-request {:error "Bad Request"})
      (respond-with/ok {:token (bs/dumps user auth-key)}))))

(defn create-new-user [email username password]
  (let [refresh-token   (str (java.util.UUID/randomUUID))
        hashed-password (hasher/make-password password)
        new-user        (query/insert-user<! {:email         email
                                              :username      username
                                              :password      hashed-password
                                              :refresh_token refresh-token})
        permission      (query/insert-permission-for-user<! {:userid     (:id new-user)
                                                             :permission "basic"})]
    (respond-with/created {:username (str (:username new-user))})))

(defn create-user-response [email username password]
  (let [username-query (query/get-registered-user-by-username {:username username})
        email-query    (query/get-registered-user-by-email {:email email})]
    (cond
      (and (not-empty username-query) (not-empty email-query)) (respond-with/conflict {:error "Username and Email already exist"})
      (not-empty username-query) (respond-with/conflict {:error "Username already exists"})
      (not-empty email-query) (respond-with/conflict {:error "Email already exists"})
      :else (create-new-user email username password))))

(defn delete-user [id]
  (let [deleted-user (query/delete-user! {:id id})]
    (if (not= 0 deleted-user)
      (respond-with/ok {:message (format "User id %d successfully removed" id)})
      (respond-with/not-found {:error "Userid does not exist"}))))

(defn delete-user-response [request id]
  (let [auth (get-in request [:identity :permissions])
        deleting-self (= id (get-in request [:identity :id]))]
    (if (or (.contains auth "admin") deleting-self)
      (delete-user id)
      (respond-with/unauthorized {:error "Not authorized"}))))

(defn delete-user-permission [id permission]
  (let [deleted-permission (query/delete-user-permission! {:userid id :permission permission})]
    (if (not= 0 deleted-permission)
      (respond-with/ok {:message (format "Permission '%s' for user %d successfully removed" permission id)})
      (respond-with/not-found {:error (format "User %s does not have %s permission" id)}))))

(defn delete-user-permission-response [request id permission]
  (let [auth (get-in request [:identity :permissions])]
        (if (.contains auth "admin")
          (delete-user-permission id permission)
          (respond-with/unauthorized {:error "Not authorized"}))))

(defn add-user-permission [id permission]
  (let [added-permission (try
                           (query/insert-permission-for-user<! {:userid id :permission permission})
                           (catch Exception e 0))]
    (if (not= 0 added-permission)
      (respond-with/ok {:message (format "Permission '%s' for user %d successfully added" permission id)})
      (respond-with/not-found {:error (format "Permission '%s' does not exist" permission)}))))

(defn add-user-permission-response [request id permission]
  (let [auth (get-in request [:identity :permissions])]
        (if (.contains auth "admin")
          (add-user-permission id permission)
          (respond-with/unauthorized {:error "Not authorized"}))))

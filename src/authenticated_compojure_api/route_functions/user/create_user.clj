(ns authenticated-compojure-api.route-functions.user.create-user
  (:require [authenticated-compojure-api.queries.query-defs :as query]
            [buddy.hashers :as hashers]
            [ring.util.http-response :as respond]))

(defn create-new-user
  "Create user with `email`, `username`, `password`"
  [email username password]
  (let [hashed-password (hashers/encrypt password)
        new-user        (query/insert-registered-user! query/db {:email         email
                                                                 :username      username
                                                                 :password      hashed-password})
        permission      (query/insert-permission-for-user! query/db {:userid     (:id new-user)
                                                                     :permission "basic"})]
    (respond/created {:username (str (:username new-user))})))

(defn create-user-response
  "Generate response for user creation"
  [email username password]
  (let [username-query   (query/get-registered-user-by-username query/db {:username username})
        email-query      (query/get-registered-user-by-email query/db {:email email})
        email-exists?    (not-empty email-query)
        username-exists? (not-empty username-query)]
    (cond
      (and username-exists? email-exists?) (respond/conflict {:error "Username and Email already exist"})
      username-exists?                     (respond/conflict {:error "Username already exists"})
      email-exists?                        (respond/conflict {:error "Email already exists"})
      :else                                (create-new-user email username password))))


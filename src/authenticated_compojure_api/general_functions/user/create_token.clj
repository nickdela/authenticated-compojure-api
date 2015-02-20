(ns authenticated-compojure-api.general-functions.user.create-token
  (:require [environ.core :refer [env]]
            [buddy.sign.generic :as bs]))

(defn create-token [user]
  (let [stringify-user (-> user
                           (update-in [:username] str)
                           (update-in [:email] str))
        token-contents (select-keys stringify-user [:permissions :username :email :id])]
    (bs/dumps token-contents (env :auth-key))))

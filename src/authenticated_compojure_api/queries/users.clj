(ns authenticated-compojure-api.queries.users)

;; ============================================
;; Stubs in place of a real database
;; ============================================
(def example-users [{:userid 1 :access "Admin" :username "JarrodCTaylor" :password "password1" :refresh-token "zeRqCTZLoNR8j0irosN9"}
                    {:userid 2 :access "User"  :username "Everyman"      :password "password2" :refresh-token "1HN05Az5P0zUhDDRzdcg"}])

(defn get-user-by-password
  [username password]
  (let [user-filter (fn [the-user] (and (= (:username the-user) username)
                                        (= (:password the-user) password)))
        user (filter user-filter example-users)]
    (if (empty? user) false (first user))))

(defn get-user-by-keyword
  [lookup-key lookup-value]
  (let [user (filter #(= (lookup-key %) lookup-value) example-users)]
    (first user)))

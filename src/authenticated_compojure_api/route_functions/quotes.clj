(ns authenticated-compojure-api.route-functions.quotes
  (:require [authenticated-compojure-api.queries.quotes :refer [add-quote
                                                                get-quote-by-keyword
                                                                remove-quote]]
            [ring.util.http-response :refer [not-found ok]]))

(defn get-specific-quote-response [id]
  (let [the-quote (get-quote-by-keyword :quoteid id)]
    (if (empty? the-quote)
      (not-found {:error "Not Found"})
      (ok the-quote))))

(defn post-new-quote-response [author quote-string]
  (let [id (add-quote author quote-string)]
    (ok (get-quote-by-keyword :quoteid id))))

(defn delete-quote-response [id]
  (do
    (remove-quote id)
    (ok {:message "Gone"})))

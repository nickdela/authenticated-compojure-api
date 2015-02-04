(ns authenticated-compojure-api.route-functions.quotes
  (:require [authenticated-compojure-api.queries.query-defs :as query]
            [ring.util.http-response :refer [not-found ok unauthorized]]))

(defn get-specific-quote-response [id]
  (let [the-quote (query/get-quote {:id id})]
    (if (empty? the-quote)
      (not-found {:error "Not Found"})
      (ok (first the-quote)))))

(defn post-new-quote-response [author quote-string]
  (ok (query/insert-quote<! {:author author :quote quote-string})))

(defn delete-quote [id]
  (query/delete-quote! {:id id})
  (ok {:message (format "Quote id %d successfully removed" id)}))

(defn delete-quote-response [request id]
  (let [auth (get-in request [:identity :permissions])]
    (if (.contains auth "admin")
      (delete-quote id)
      (unauthorized {:error "Not authorized."}))))

(defn update-quote-response [id author quote-string]
  (let [old-quote        (first (query/get-quote {:id id}))
        new-author       (if (empty? author) (:author old-quote) author)
        new-quote-string (if (empty? quote-string) (:quote old-quote) quote-string)]
    (ok (query/update-quote<! {:id id :author author :quote new-quote-string}))))

(ns authenticated-compojure-api.routes.quotes
  (:require [schema.core :as s]
            [ring.util.http-response :refer [ok]]
            [authenticated-compojure-api.queries.quotes :refer :all]
            [compojure.api.sweet :refer :all]))


;; ============================================
;; Schema(s)
;; ============================================
(s/defschema Quote {:quoteid Long :author String :quote String})


;; ============================================
;; Routes
;; ============================================
(defroutes* quote-routes
  (context "/api" []

    (GET* "/quotes" []
      :return [Quote]
      :summary "Returns an array of all available quotes."
      (ok @quotes))

    (GET* "/quotes/:id" []
      :return Quote
      :path-params [id :- Long]
      :summary "Returns the quote with the specified id"
      (ok (get-quote-by-keyword :quoteid id)))

    (POST* "/quotes" []
      :return Quote
      :form-params [author :- String quote-string :- String]
      :summary "Create a new quote provided the author and quote strings"
      (let [id (add-quote author quote-string)]
        (ok (get-quote-by-keyword :quoteid id))))))

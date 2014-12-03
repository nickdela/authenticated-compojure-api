(ns authenticated-compojure-api.routes.quotes
  (:require [authenticated-compojure-api.queries.quotes :refer :all]
            [authenticated-compojure-api.route-functions.quotes :refer [delete-quote-response
                                                                        get-specific-quote-response
                                                                        post-new-quote-response]]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer [ok]]
            [schema.core :as s]))


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
      (get-specific-quote-response id))

    (POST* "/quotes" []
      :return Quote
      :body-params [author :- String quote-string :- String]
      :summary "Create a new quote provided the author and quote strings"
      (post-new-quote-response author quote-string))

    (DELETE* "/quotes/:id" []
      :path-params [id :- Long]
      (delete-quote-response))))

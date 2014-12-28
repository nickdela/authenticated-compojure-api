(ns authenticated-compojure-api.middleware.cors)

;; ============================================
;  Allow requests from all origins
;; ============================================
(defn cors-mw [handler]
  (fn [request]
    (let [response (handler request)]
      (update-in response
                 [:headers "Access-Control-Allow-Origin"]
                 (fn [_] "*")))))

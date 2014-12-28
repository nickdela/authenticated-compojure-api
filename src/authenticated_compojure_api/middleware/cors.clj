(ns authenticated-compojure-api.middleware.cors)

;; ============================================
;  Allow requests from all origins
;; ============================================
(defn cors-mw [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
           (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
           (assoc-in [:headers "Access-Control-Allow-Headers"] "Authorization")))))

(ns authenticated-compojure-api.middleware.cors)

(defn cors-mw
  "Cross-origin Resource Sharing (CORS) middleware \\
  Allow requests from all origins"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
          (assoc-in [:headers "Access-Control-Allow-Methods"] "GET, PUT, PATCH, POST, DELETE, OPTIONS")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "Authorization, Content-Type")))))

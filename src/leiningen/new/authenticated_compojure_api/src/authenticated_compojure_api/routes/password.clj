(ns {{ns-name}}.routes.password
  (:require
    [compojure.api.sweet :refer [context POST]]
    [{{ns-name}}.specs :as specs]
    [{{ns-name}}.general-functions.validations :as validations]
    [{{ns-name}}.middleware.cors :refer [cors-mw]]
    [{{ns-name}}.route-functions.password.password-reset :refer [password-reset-response]]
    [{{ns-name}}.route-functions.password.request-password-reset :refer [request-password-reset-response]]))

(def password-routes
  (context "/api/v1/password" []
           :tags ["Password"]
           :return {:message ::specs/message}
           :middleware [cors-mw]
           :coercion validations/spec

    (POST "/reset-request" []
          :body-params [userEmail :- ::specs/email
                        fromEmail :- ::specs/email
                        subject :- ::specs/subject
                        {emailBodyHtml :- ::specs/emailBodyHtml ""}
                        {emailBodyPlain :- ::specs/emailBodyPlain ""}
                        responseBaseLink :- ::specs/responseBaseLink]
          :summary "Request a password reset for the registered user with the matching email"
          :description "The `response-base-link` will get a reset key appended to it and then the
                        link itself will be appended to the email body. The reset key will be valid
                        for 24 hours after creation. *NOTE* do not use a fromEmail address ending
                        with @gmail.com because of the DMARC policy. It is recommended to use a custom
                        domain you own instead"
          (request-password-reset-response userEmail fromEmail subject emailBodyPlain emailBodyHtml responseBaseLink))

    (POST "/reset-confirm" []
           :body-params [resetKey :- ::specs/resetKey
                         newPassword :- ::specs/newPassword]
           :summary "Replace an existing user password with the newPassowrd given a valid resetKey"
           (password-reset-response resetKey newPassword))))

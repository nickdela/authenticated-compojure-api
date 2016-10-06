{:dev-env-vars  {:env {:database-url  "postgres://{{sanitized}}_user:password1@127.0.0.1:5432/{{sanitized}}?stringtype=unspecified"
                       :sendinblue-user-login    "You@Something.com"
                       :sendinblue-user-password "sendinblue"
                       :auth-key      "theSecretKeyUsedToCreateAndReadTokens"}}
 :test-env-vars {:env {:database-url  "postgres://{{sanitized}}_user:password1@127.0.0.1:5432/{{sanitized}}_test?stringtype=unspecified"
                       :auth-key      "theSecretKeyUsedToCreateAndReadTokens"}}}

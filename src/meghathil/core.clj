(ns meghathil.core
  (:require [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn])
  (:gen-class))
(import '(com.google.api.client.googleapis.auth.oauth2
          GoogleAuthorizationCodeFlow GoogleCredential
          GoogleTokenResponse GoogleAuthorizationCodeFlow$Builder)
        '(com.google.api.client.http FileContent HttpTransport)
        '(com.google.api.client.http.javanet NetHttpTransport)
        '(com.google.api.client.json JsonFactory)
        '(com.google.api.client.json.jackson JacksonFactory)
        '(com.google.api.services.drive Drive DriveScopes Drive$Builder)
        '(com.google.api.services.drive.model File))

(require 'clojure.java.io)

(def CLIENT_ID "355829055312-gqarc8eohg441ls0e757esgd6r5vmh3p.apps.googleusercontent.com")
(def CLIENT_SECRET "2mqNDFHz-EfSCQB4mm1X6SDZ")

(def REDIRECT_URI "urn:ietf:wg:oauth:2.0:oob")

(defn get-auth-code [url]
  (println "Please open the following URL in your browser "
           "then type the authorization code:")
  (println (str "  " url))
  (read-line))

(defn insert-file []
  (let [body (new File)]
    (doto body
      (.setTitle "Readme")
      (.setDescription "A test document")
      (.setMimeType "text/plain"))
    body))

(defn do-the-stuff
  []
  (let [httpTransport (new NetHttpTransport)
        jsonFactory (new JacksonFactory)
        flow (-> (new GoogleAuthorizationCodeFlow$Builder httpTransport
                      jsonFactory CLIENT_ID CLIENT_SECRET
                      (list (. DriveScopes DRIVE)))
                 (.setAccessType "online")
                 (.setApprovalPrompt "auto")
                 (.build))
        url (-> (.newAuthorizationUrl flow)
                (.setRedirectUri REDIRECT_URI)
                (.build))
        code (get-auth-code url)
        response (-> (.newTokenRequest flow code)
                     (.setRedirectUri REDIRECT_URI)
                     (.execute))
        credential (-> (new GoogleCredential)
                       (.setFromTokenResponse response))
        ;; Create a new authorized API client
        service (-> (new Drive$Builder httpTransport
                         jsonFactory credential) (.build))
        body (insert-file)
        fileContent (clojure.java.io/file "README.md")
        mediaContent (new FileContent "text/plain" fileContent)
        file (-> (.files service)
                 (.insert body mediaContent)
                 (.execute))]
    (println "File ID:" (.getId file))))

(defn get-conf
  "simple read all the configuration, will decide what to do with in some other place"
  [file-path]
  (edn/read-string (slurp file-path)))

(defn -main
  "I don't do a whole lot."
  [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [conf (get-conf "conf.edn")]
    (log/info "Hello, World!")
    (log/info "Conf" conf)
    (fs/walk #(log/info %1 %2 %3) (:dir conf)))
  (do-the-stuff))

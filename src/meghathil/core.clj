(ns meghathil.core
  (:require [me.raynes.fs :as fs]
            [pantomime.mime :refer [mime-type-of]]
            ;; [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [cheshire.core :refer [parse-string generate-string]]
            [clj-oauth2.client :as oauth2])
  (:gen-class))
(require 'clojure.java.io)

(def google-com-oauth2
  (edn/read-string (slurp
                    (clojure.java.io/resource "google-com-oauth2.edn"))))

(def auth-req
  (oauth2/make-auth-request google-com-oauth2))

(def url-google-file "https://www.googleapis.com/drive/v2/files/")
(def url-google-upload-file "https://www.googleapis.com/upload/drive/v2/files?uploadType=media")

(defn get-auth-code [url]
  (println "Please open the following URL in your browser "
           "then type the authorization code:")
  (println url)
  (read-line))

(defn get-google-credential []
  (let [url (:uri auth-req)
        code (get-auth-code url)]
    (oauth2/get-access-token google-com-oauth2
                             {:code code} auth-req)))

(defn google-upload-file [credential file]
  (oauth2/post url-google-upload-file
               {:oauth2 credential
                :headers {"Content-Type" (mime-type-of file)}
                ;; "text/plain"
                :throw-exceptions false
                :body file}))

(defn google-insert-file-with-metadata [credential file metadata]
  (let [upload_file (google-upload-file credential file)]
    (if (= (:status upload_file) 200)
      (let [response (parse-string (:body upload_file))
            fileId (response "id")]
        (oauth2/put (str url-google-file fileId)
                    {:oauth2 credential
                     :throw-exceptions false
                     :body metadata ;; "{\"title\": \"Readme\"}"
                     :content-type :json})))))

(defn sync-folder [folder sub-folders files]
  ;; (let [credential (get-google-credential)]
        ;; Create a new authorized API client
        ;; list_files (oauth2/get
        ;;             url-google-file
        ;;             {:oauth2 credential})
    ;; (println "File ID:" (.getId file))
    (println folder sub-folders)
    (for [file files]
      (google-insert-file-with-metadata
       credential (clojure.java.io/file  folder file)
       (generate-string {:title file}))))

(defn get-conf
  "simple read all the configuration, will decide what to do with in some other place"
  [file-path]
  (edn/read-string (slurp file-path)))

(defn -main
  "I don't do a whole lot."
  [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [conf (get-conf "conf.edn")]
    ;; (log/info "Hello, World!")
    ;; (log/info "Conf" conf)
    (fs/walk sync-folder (:dir conf))))

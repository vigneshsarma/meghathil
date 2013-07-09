(ns meghathil.core
  (:require [me.raynes.fs :as fs]
            ;; [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [cheshire.core :refer [parse-string]]
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
                :headers {"Content-Type" "text/plain"}
                :throw-exceptions false
                :body file}))
(defn google-insert-file-with-metadata [credential file metadata]
  (let [upload_file (google-upload-file credential file)]
    (if (= (:status upload_file) "200")
      (let [response (parse-string (:body upload_file))
            fileId ("fileId" response)]
        (oauth2/put
         (str url-google-file fileId)
         {:oauth2 credential
          :throw-exceptions false
          :file-params metadata})))))

(defn insert-file []
  (let [body (new File)]
    (doto body
      (.setTitle "Readme")
      (.setDescription "A test document")
      (.setMimeType "text/plain"))
    body))

(defn do-the-stuff []
  (let [
        credential (get-google-credential)
        ;; Create a new authorized API client
        list_files (oauth2/get
                    url-google-file
                    {:oauth2 credential})
        upload_file (oauth2/post
                     url-google-file
                     {:oauth2 credential
                      :mutlipart [{:name "Readme"
                                   :content (clojure.java.io/file "README.md")}]
                      :form-params {:title "Readme"
                                    :mime-type "text/plain"
                                    :description "A test document"}})]
    ;; (println "File ID:" (.getId file))
    (println (:body list_files) (:body upload_file))))

(defn get-conf
  "simple read all the configuration, will decide what to do with in some other place"
  [file-path]
  (edn/read-string (slurp file-path)))

(defn -main
  "I don't do a whole lot."
  [& args]
  (alter-var-root #'*read-eval* (constantly false))
  ;; (let [conf (get-conf "conf.edn")]
    ;; (log/info "Hello, World!")
    ;; (log/info "Conf" conf)
    ;; (fs/walk #(log/info %1 %2 %3) (:dir conf)))
  (do-the-stuff)
  )

(def credential {:access-token "ya29.AHES6ZTiboGbzeJbgTSBOZqs9udgildd_GcsCnpit2j-pvXy0FVhXg", :token-type "Bearer", :query-param :access_token, :params {:expires_in 3600, :refresh_token "1/B1eZmo4NjtULiRHyKFV12HQPTuYj0aPQEKGJ9keqOj0"}})

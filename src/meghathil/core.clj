(ns meghathil.core
  (:require [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn]))

(defn get-conf
  "simple read all the configuration, will decide what to do with in some other place"
  [file-path]
  (edn/read-string (slurp file-path)))

(defn -main
  "I don't do a whole lot."
  [& args]
  (let [conf (get-conf "conf.edn")]
    (log/info "Hello, World!")
    (log/info "Conf" conf)
    (fs/walk #(log/info %1 %2 %3) (:dir conf))))

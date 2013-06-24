(ns meghathil.core
  (:require [me.raynes.fs :as fs]))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!")
  (fs/walk println fs/*cwd*))

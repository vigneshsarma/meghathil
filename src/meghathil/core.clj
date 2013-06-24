(ns meghathil.core
  (:require [me.raynes.fs :as fs]
            [clojure.tools.logging :as log]))

(defn -main
  "I don't do a whole lot."
  [& args]
  (log/info "Hello, World!")
  (fs/walk #(log/info %1 %2 %3) fs/*cwd*))

(ns fae.print)

(defn lifecycle [msg]
  (js/console.log (str "%c" msg) "color: white; background:blue; padding: 2px; font-weight: bold;"))

(defn events [msg]
  (js/console.log (str "%c" msg) "color: black; background:yellow; padding: 2px; font-weight: bold;"))

(defn debug [msg]
  (js/console.log (str "%c" msg) "color: white; background:black; padding: 2px; font-weight: bold;"))

(defn tap [v]
  (debug v)
  v)
(ns authenticated-compojure-api.queries.quotes)

(def quotes (atom [{:quoteid 1 :author "Oscar Wilde" :quote "Always forgive your enemies; nothing annoys them so much."}
                   {:quoteid 2 :author "Plutarch" :quote "When Alexander the Great saw the breadth of his domain, he wept for there were no more worlds to conquer."}]))

(defn get-quote-by-keyword
  [lookup-key lookup-value]
  (let [the-quote (filter #(= (lookup-key %) lookup-value) @quotes)]
    (first the-quote)))

(defn next-id []
  (->>
    @quotes
    (map :quoteid)
    (apply max)
    (+ 1)))

(defn add-quote [author quote-string]
  (let [id (next-id)]
    (swap! quotes conj {:quoteid id :author author :quote quote-string})
    id))

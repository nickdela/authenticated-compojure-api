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

(defn remove-quote [quoteid]
  (swap! quotes (fn [the-quote] (remove #(= (:quoteid %) quoteid) the-quote))))

(defn update-quote-if-id-match [the-quote quote-id author new-quote-string]
  (if (= (:quoteid the-quote) quote-id)
    (assoc the-quote :author author :quote new-quote-string)
    the-quote))

(defn update-quote [quote-id author new-quote-string]
  (swap! quotes (fn [quotes]
                  (for [the-quote quotes]
                    (update-quote-if-id-match the-quote quote-id author new-quote-string)))))

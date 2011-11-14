(ns monger.pagination)

(defn offset-for
  [^long page ^long per-page]
  (* per-page
     (- (max page 1) 1)))

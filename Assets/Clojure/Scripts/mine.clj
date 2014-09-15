(ns mine
  (:use unity.core)
  (:import [UnityEngine Resources Debug RequireComponent Rigidbody]))

(defcomponent ^{RequireComponent Rigidbody} VoxelTexture
  [^System.String texure]
  (Start [this]
         (Debug/Log (str "Textures/" texure))
         (set! (.. this renderer material mainTexture) 
               (Resources/Load (str "Textures/" texure)))))

(defn one-of [& branches]
  (rand-nth branches))

(defn texture-at-height
  ([height max-height] (texture-at-height (/ height max-height)))
  ([height]
    (cond
      (<= height 0.25)
      (one-of
        "bedrock")
      (<= 0.25 height 0.3)
      (one-of
        "gravel")
      (<= 0.3 height 0.5)
      (one-of
        "grass_top")
      (<= 0.5 height 0.6)
      (one-of
        "cobblestone"
        "cobblestone_mossy")
      (<= 0.6 height 0.75)
      (one-of
        "snow")
      (<= 0.75 height)
      (one-of
        "ice"))))

(defmacro noise
    ([x] `(Mathf/PerlinNoise (+ 0.1 ~x) 0))
    ([x y] `(Mathf/PerlinNoise (+ 0.1 ~x) ~y)))

(let [vx (GameObject/Find "Voxel"), dim 50]
    (doseq [x (range dim)
            z (range dim)]
      (let [height (int (+ 1 (* 20 (noise 
                                     (* 0.05 x)
                                     (* 0.05 z)))))]
        (doseq [y (range height)]
          (if (= y (- height 1))
            (let [tex (Resources/Load (str "Textures/" (texture-at-height y 20)))
                  obj (GameObject/Instantiate vx)
                  ren (.GetComponent obj UnityEngine.Renderer)]
              (populate-game-object! obj {:name (str "Voxel " [x y z])
                                          :transform [{:position [x y z]}]})
              (set! (.. ren material mainTexture) tex)))))))
#!/bin/bash

import sys


import numpy as np
import tensorflow as tf


meta = eval(sys.argv[1])
model = tf.keras.models.load_model("/Users/peterfrancis/Programming/gin-rummy-AI/python/nnhe-simple-1.h5")


pred = model.predict(np.array([meta]))[0]

print(*pred, sep="\n")

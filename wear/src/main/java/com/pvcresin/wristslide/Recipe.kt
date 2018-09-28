package com.pvcresin.wristslide

data class Recipe(val image: Int,
                  val dishName: String,
                  val comment: String,
                  val ingredients: List<String>,
                  val quantities: List<String>,
                  val procedures: List<String>)
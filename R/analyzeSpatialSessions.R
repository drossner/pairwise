install.packages("ggplot2")
install.packages("dplyr")

library(ggplot2)
library(dplyr)


spatial_Session_data <- read.csv("C:/Users/Chris/Documents/Uni/Arbeit/pairwise/R/src/Spatial_Data.csv", sep = ",", fileEncoding = "UTF-8")
as.character(spatial_Session_data$ConceptB)

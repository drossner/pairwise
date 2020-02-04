install.packages("ggplot2")
install.packages("dplyr")

require(ggplot2)
require(dplyr)

library(ggplot2)
library(dplyr)


spatial_Session_data <- read.csv("C:/Users/Chris/Documents/Uni/Arbeit/pairwise/R/src/Spatial_Data.csv", sep = ",", fileEncoding = "UTF-8")


qqnorm(spatial_Session_data$AvgPercentage)
qqline(spatial_Session_data$AvgPercentage)

plot(density(spatial_Session_data$AvgPercentage))

x <- spatial_Session_data$AvgPercentage
den <- density(log(spatial_Session_data$AvgPercentage))
den$x <- exp(den$x)
plot(den, log = "x")

avg <- spatial_Session_data$AvgPercentage
avg
plot(density(avg))


boxplot(avg)
shapiro.test(avg)
hist(avg)



boxplot(spatial_Session_data$Percentage)

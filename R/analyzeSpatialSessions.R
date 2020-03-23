install.packages("ggplot2")
install.packages("dplyr")

require(ggplot2)
require(dplyr)

library(ggplot2)
library(dplyr)
library(nortest)
library(exptest)

spatial_Session_data <- read.csv("./src/Spatial_Data.csv", sep = ",", fileEncoding = "UTF-8")
boxplot(spatial_Session_data$Duration/1000 ~ spatial_Session_data$QstNr, xlab="instance number", ylab="duration in s")
  mean(spatial_Session_data$Duration/1000)
median(spatial_Session_data$Duration/1000)

boxplot(spatial_Session_data$TotalClicks ~ spatial_Session_data$QstNr, xlab="instance number", ylab="total clicks")
mean(spatial_Session_data$TotalClicks)
median(spatial_Session_data$TotalClicks)

attach(spatial_Session_data)
plot(Duration,TotalClicks)
abline(lm(TotalClicks~Duration), col="red") # regression line (y~x)
lines(lowess(Duration,TotalClicks), col="blue") # lowess line (x,y)

model = lm(Duration ~ TotalClicks, data=faithful)
summary(model)

boxplot(TotalClicks ~ QstNr)

detach(spatial_Session_data)

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
lillie.test(avg)
hist(avg)

boxplot(spatial_Session_data$Percentage)

spatComp  <- read.csv("src/spat-comb.csv", fileEncoding = "utf8")
hist(spatComp$ratings)
boxplot(spatComp$ratings)
shapiro.test(spatComp$ratings)
plot(density(spatComp$dist))
     
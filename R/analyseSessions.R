install.packages("ggplot2")
install.packages("dplyr")
install.packages("hrbrthemes")

###Libraries
library(ggplot2)
library(dplyr)
library(hrbrthemes)
library(nortest)
library(exptest)

#hrbrthemes::import_roboto_condensed()

### import CSV-Files
sessions <- read.csv("R/src/completedSessions.csv", sep = ",", fileEncoding = "UTF-8")
singleSessions <- read.csv("R/src/singleSessions.csv", sep = ",", fileEncoding = "UTF-8")

### subsets für alle ersten, zweiten und dritten vergleiche
### für den Rest genauso vorgehen wie bei den anderen nur modulo anpassen
firstComparisons <- subset(singleSessions, 1:nrow(singleSessions)%%8==1)
secondComparisons <- subset(singleSessions, 1:nrow(singleSessions)%%8==2)
thirdComparisons <- subset(singleSessions, 1:nrow(singleSessions)%%8==3)
#lastComparisons <- subset(singleSessions, 1:nrow(singleSessions)%%8==0)

avg_duration_1 <- mean(firstComparisons$Duration)
avg_duration_2 <- mean(secondComparisons$Duration)
avg_duration_3 <- mean(thirdComparisons$Duration)
#avg_duration_8 <- mean(lastComparisons$Duration)


######################################################################
### Plot für die durchschnittliche Dauer pro Session als Barchart  ###
######################################################################
s = sessions$Session
avg_duration <- sessions$Avg.Duration
df <- data.frame(s, avg_duration)
df

p_col <- ggplot(data=df, aes(s, avg_duration))+
  geom_col()+
  xlab(NULL)+
  ylab("Average Durations")+
  coord_flip()
p_col


###########################################################
### Plots und Tests für Normalverteilung der Vergleiche ###
###########################################################

### Q-Q Plot, wenn die Punkte näherungsweise an der Linie sind dann liegt eine Normalverteilung vor
qqnorm(firstComparisons$Duration)
qqline(firstComparisons$Duration)
qqnorm(secondComparisons$Duration)
qqline(secondComparisons$Duration)
qqnorm(thirdComparisons$Duration)
qqline(thirdComparisons$Duration)
qqnorm(singleSessions$Duration)
qqline(singleSessions$Duration)

##Daniel Tests##
qqnorm(log(singleSessions$Duration))
exptest <- qexp(ppoints(length(singleSessions$Duration)))
qqplot(exptest, singleSessions$Duration)
qqplot(exptest, log(singleSessions$Duration))

ggplot(data = singleSessions) +
  geom_boxplot(mapping = aes(y=singleSessions$Duration)) +
  ylab("Duration") +
  ggtitle("Boxplot for the duration of all comparisons")


boxplot(log(singleSessions$Duration) ~ singleSessions$qstnr) #loged
boxplot(singleSessions$Duration/1000 ~ singleSessions$qstnr) #not loged
boxplot(singleSessions$Duration/1000) #not loged
mean(singleSessions$Duration/1000) #average
median(singleSessions$Duration/1000) #median


plot(density(log(singleSessions$Duration), adjust= 0.9), col=1) 
plot(density(singleSessions$Duration, adjust= 0.9), col=1) #some high values

shapiro.test(log(singleSessions$Duration))
shapiro.test(singleSessions$Duration) #without log

pearson.test(log(singleSessions$Duration))
pearson.test(adjust = TRUE, log(singleSessions$Duration))

ep.exp.test(singleSessions$Duration) #Test for exponentiality of Epps and Pulley
shapiro.exp.test(singleSessions$Duration)
frozini.exp.test(log(singleSessions$Duration))

##Daniel Tests ende##

### Boxplot, Strich in der Mitte ist der Median, wenn dieser zentral in der Box liegt,
### liegt ebenfalls eine Normalverteilung vor
ggplot(data = firstComparisons) +
  geom_boxplot(mapping = aes(y=firstComparisons$Duration)) + 
  ylab("Duration") + 
  ggtitle("Boxplot for the duration of all first comparisons") +
  theme_ipsum_rc()
ggplot(data = secondComparisons) + 
  geom_boxplot(mapping = aes(y=secondComparisons$Duration)) + 
  ylab("Duration") + 
  ggtitle("Boxplot for the duration of all second comparisons") +
  theme_ipsum()
ggplot(data = thirdComparisons) + 
  geom_boxplot(mapping = aes(y=thirdComparisons$Duration)) + 
  ylab("Duration") + 
  ggtitle("Boxplot for the duration of all third comparisons") +
  theme_ipsum()
#ggplot(data = lastComparisons) + 
 # geom_boxplot(mapping = aes(y=lastComparisons$Duration)) + 
  #ylab("Duration") + 
  #ggtitle("Boxplot for the duration of all eight comparisons")


### Shapiro Wilk Tests für die ersten, zweiten, dritten,... Vergleiche
### speziell für Datensätze zwischen 3 und 5000
### Hat eine gewissen Aussagekraft, allerdings ist bei größeren Werten der 
### Solmogorow-Smirnow- oder Chi-Quadrat-Test besser geeignet
shapiro.test(firstComparisons$Duration)
shapiro.test(secondComparisons$Duration)
shapiro.test(thirdComparisons$Duration)
#shapiro.test(lastComparisons$Duration)


### DichteFunktion der jeweiligen Vergleiche, Glockenkurve zum Ablesen ob eine
### Normalverteilung vorliegt
plot(density(firstComparisons$Duration))
ggplot(data = firstComparisons, aes(x=firstComparisons$Duration)) + 
  geom_density(kernel = "gaussian") + 
  xlab("Duration") + 
  ggtitle("density of all first durations")

plot(density(secondComparisons$Duration))
ggplot(data = secondComparisons, aes(x=secondComparisons$Duration)) + 
  geom_density(kernel = "gaussian") + 
  xlab("Duration") + 
  ggtitle("density of all second durations")

plot(density(thirdComparisons$Duration))
ggplot(data = thirdComparisons, aes(x=thirdComparisons$Duration)) + 
  geom_density(kernel = "gaussian") + 
  xlab("Duration") + 
  ggtitle("density of all third durations")


### Das selbe nochmal nur für komplett alle Vergleiche
shapiro.test(singleSessions$Duration)
ggplot(data = singleSessions) + geom_boxplot(mapping = aes(y=singleSessions$Duration)) + 
  ylab("Duration") + 
  ggtitle("Boxplot of all durations")
qqnorm(singleSessions$Duration)
qqline(singleSessions$Duration)
plot(density(singleSessions$Duration))


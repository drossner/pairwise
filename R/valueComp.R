library(ggplot2)
library(dplyr)
library(tidyr)

singleSessions <- read.csv("R/src/singleSessions.csv", sep = ",", fileEncoding = "UTF-8")

filteredSimple <- singleSessions %>%
  select(ConceptA, ConceptB, Rating) %>%
  mutate(Ordered = ifelse(as.character(ConceptB) < as.character(ConceptA), paste(as.character(ConceptB),as.character(ConceptA),sep = ";"), paste(as.character(ConceptA),as.character(ConceptB), sep=";"))) %>%
  group_by(Ordered) %>%
  summarise(Rating = (mean(Rating)/10)-0.1, n = n()) %>%
  arrange(Ordered)
  #separate(Ordered, c("A", "B"), ";")

spatial_Session_data <- read.csv("R/src/Spatial_Data.csv", sep = ",", fileEncoding = "UTF-8")

filteredSpatial <- spatial_Session_data %>%
  select(ConceptA, ConceptB, AvgPercentage) %>%
  mutate(Ordered = ifelse(as.character(ConceptB) < as.character(ConceptA), paste(as.character(ConceptB),as.character(ConceptA), sep=";"), paste(as.character(ConceptA),as.character(ConceptB), sep=";"))) %>%
  group_by(Ordered) %>%
  summarise(n=n(), Rating = 1-mean(AvgPercentage)) %>%
  arrange(Ordered)
  #separate(Ordered, c("A", "B"), ";")

filteredCombined <- inner_join(filteredSimple, filteredSpatial, by = c("Ordered" = "Ordered")) %>%
  mutate(Stress = abs(Rating.x - Rating.y)) %>%
  mutate(n = pmin(n.x,n.y)) %>%
  filter(n > 1) %>%
  separate(Ordered, c("A", "B"), ";")

#mean(filteredCombined$Rating.x)
mean(filteredCombined$Stress)
median(filteredCombined$Stress)

attach(filteredCombined)

boxplot(Stress ~ n)

hist((Rating.x))
plot(Rating.x, Rating.y)
abline(lm(Rating.x ~ Rating.y))


norm_values <- read.csv("R/src/postcsv.csv", sep = ",", fileEncoding = "UTF-8")

qqnorm(norm_values$spat)
qqline(norm_values$spat)
density(norm_values$spat)

qqnorm(norm_values$comp)
qqline(norm_values$comp)
density(norm_values$comp)

plot(x=norm_values$spat, y=norm_values$comp)

cor(norm_values$spat, norm_values$comp)

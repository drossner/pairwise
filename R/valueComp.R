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


# new eval for thesis

norm_values <- read.csv("R/src/postcsv.csv", sep = ",", fileEncoding = "UTF-8")

qqnorm(norm_values$spat)
qqline(norm_values$spat)
density(norm_values$spat)

qqnorm(norm_values$comp)
qqline(norm_values$comp)
density(norm_values$comp)

plot(x=norm_values$spat, y=norm_values$comp)

cor(norm_values$spat, norm_values$comp)
cor.test(norm_values$spat, norm_values$comp)

# inter rater aggrement
library("irr")
comp_values <- read.csv("R/csv-export/Comparison_Information.csv", sep = ",", fileEncoding = "UTF-8")

ratings <- comp_values %>%
  group_by(ConceptA, ConceptB) %>%
  arrange(SessionID) %>%
  #no clue how do that dynamically.. :(
  summarise(Weight_Mean = mean(Weight), n = n(), rater1 = nth(Weight, 1), rater2 = nth(Weight, 2), rater3 = nth(Weight, 3), rater4 = nth(Weight, 4))

cleaned_ratings <- ratings %>% select(contains("rater"))

icc(
  cleaned_ratings[,-1], model = "twoway",
  type = "agreement", unit = "single"
)

# Koo and Li (2016) gives the following suggestion for interpreting ICC (Koo and Li 2016):

# below 0.50: poor
# between 0.50 and 0.75: moderate
# between 0.75 and 0.90: good
# above 0.90: excellent



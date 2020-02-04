# Title     : TODO
# Objective : TODO
# Created by: daniel
# Created on: 19.12.19

###Libraries
library(ggplot2)
library(dplyr)
library(tidyr)

sessions <- read.csv("src/completedSessions.csv", sep = ",", fileEncoding = "UTF-8")
singleSessions <- read.csv("src/singleSessions.csv", sep = ",", fileEncoding = "UTF-8")

filtered <- singleSessions %>%
  select(ConceptA, ConceptB, Rating) %>%
  mutate(Ordered = ifelse(as.character(ConceptB) < as.character(ConceptA), paste(as.character(ConceptB),as.character(ConceptA)), paste(as.character(ConceptA),as.character(ConceptB)))) %>%
  group_by(Ordered) %>%
  summarise(Rating = mean(Rating), n = n()) %>%
  arrange(desc(n)) %>%
  separate(Ordered, c("A", "B"), " ")
  


ggplot(data = singleSessions) +
  aes(x = Rating) +
  geom_histogram(aes(y=..density..), binwidth = 0.5) +
  geom_density(alpha=.2, fill="red")
  


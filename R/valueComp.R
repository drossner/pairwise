library(ggplot2)
library(dplyr)
library(tidyr)
library(tibble)

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

ggplot(norm_values, aes(x=name, y=spat, fill="asd"))  +
  geom_boxplot() +
  geom_jitter(color="black", size=3, alpha=0.9) +
  ggtitle("A boxplot with jitter") +
  xlab("")

norm_values %>%
  #to create index
  rownames_to_column("ID") %>%
  arrange(comp) %>%
  mutate(ID = as.numeric(ID)) %>%
  #Making it tidy
  gather(vars, value, -ID) %>%
  ggplot(aes(vars, value)) +
  geom_boxplot() +
  geom_jitter(size=1, alpha=0.8) +
  xlab("")

norm_values %>%
  ggplot(aes(x=spat, y=comp)) +
  geom_smooth(method=lm) +
  geom_point(size=1) +
  xlab("spatial rating (inferred)") + ylab("pairwise rating")

cor(norm_values$spat, norm_values$comp, method="kendall")
cor.test(norm_values$spat, norm_values$comp, method = "kendall")
cor.test(norm_values$spat, norm_values$comp, method = "spearman")

#
# 0,1-0,3 (schwach),
# 0,3-0,5 (mittel) und
# größer 0,5 (stark).

# inter rater aggrement
library("irr")
comp_values <- read.csv("R/csv-export/Comparison_Information.csv", sep = ",", fileEncoding = "UTF-8")
spat_values <- read.csv("R/csv-export/Spatial_Information.csv", sep = ",", fileEncoding = "UTF-8")

ratings <- comp_values %>%
  group_by(ConceptA, ConceptB) %>%
  arrange(SessionID) %>%
  #no clue how do that dynamically.. :(
  summarise(Weight_Mean = mean(Weight), n = n(), rater1 = nth(Weight, 1), rater2 = nth(Weight, 2), rater3 = nth(Weight, 3), rater4 = nth(Weight, 4), rater5 = nth(Weight, 5), rater6 = nth(Weight, 6), rater7 = nth(Weight, 7))

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

median(norm_values$comp)
mean(norm_values$comp)
median(norm_values$spat)
mean(norm_values$spat)

summary(spat_values %>%
  group_by(SessionID) %>%
  summarise(duration = mean(QstDuration))
)

summary(comp_values %>%
  group_by(SessionID) %>%
  summarise(duration = mean(Duration))
)

shapiro.test(norm_values$comp)
shapiro.test(norm_values$spat)


# do some test for time vs clicks

click_data <- spat_values %>%
  group_by(SessionID, QstNr) %>%
  summarise(duration = min(QstDuration) / 1000, clicks = min(ConceptClicksPerTest)) # values are the same in all rows for a given instance

click_data %>%
  ggplot(aes(x = duration, y = clicks)) +
  geom_point() + geom_smooth(method = lm) +
  xlab("Duration in seconds") + ylab("Clicks per instance")

cor.test(click_data$duration, click_data$clicks, method = "kendall") # neither clicks nor the duration has a normal distribution

# irr ?? for each concept pair



test <- comp_values %>%
  group_by(ConceptA, ConceptB) %>%
  mutate(Weight = (Weight - 1) / 9) %>%
  summarise(mean = mean(Weight), var = var(Weight))


test %>%
  arrange(ConceptA) %>%
  ggplot() +
  scale_x_discrete(drop = FALSE) +
  scale_y_discrete(drop = FALSE) +
  xlab(NULL) + ylab(NULL) +
  geom_tile(aes(x = ConceptA, y = ConceptB, fill = var)) +
  geom_tile(aes(x = ConceptB, y = ConceptA, fill = var)) +
  theme(
    # Rotate the x-axis lables so they are legible
    axis.text.x = element_text(angle = 270, hjust = 0),
    # Force the plot into a square aspect ratio
    aspect.ratio = 1,
    # Hide the legend (optional)
  )

test %>%
  arrange(ConceptA) %>%
  ggplot() +
  scale_x_discrete(drop = FALSE) +
  scale_y_discrete(drop = FALSE) +
  geom_tile(aes(x = ConceptA, y = ConceptB, fill = mean)) +
  geom_tile(aes(x = ConceptB, y = ConceptA, fill = mean)) +
  xlab(NULL) + ylab(NULL) +
  theme(
    # Rotate the x-axis lables so they are legible
    axis.text.x = element_text(angle = 270, hjust = 0),
    # Force the plot into a square aspect ratio
    aspect.ratio = 1,
    # Hide the legend (optional)
  )



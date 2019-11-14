-- apply changes
create table spatial_comparison_clicks_per_concept (
  spatial_comparison_id         bigint not null,
  value                         integer not null
);

create index ix_spatial_comparison_clicks_per_concept_spatial_comparis_1 on spatial_comparison_clicks_per_concept (spatial_comparison_id);
alter table spatial_comparison_clicks_per_concept add constraint fk_spatial_comparison_clicks_per_concept_spatial_comparis_1 foreign key (spatial_comparison_id) references spatial_comparison (id) on delete restrict on update restrict;


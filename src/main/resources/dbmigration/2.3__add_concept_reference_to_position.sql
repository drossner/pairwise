-- apply changes
alter table spatial_pos add column CONCEPT_ID bigint;

alter table spatial_pos add constraint fk_spatial_pos_concept_id foreign key (concept_id) references concept (id) on delete restrict on update restrict;


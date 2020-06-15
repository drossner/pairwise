-- apply changes
create table spatial_node_tracked (
  id                            bigserial not null,
  spatial_comparison_id         bigint not null,
  name                          varchar(255) not null,
  x                             float not null,
  y                             float not null,
  constraint pk_spatial_node_tracked primary key (id)
);

create index ix_spatial_node_tracked_spatial_comparison_id on spatial_node_tracked (spatial_comparison_id);
alter table spatial_node_tracked add constraint fk_spatial_node_tracked_spatial_comparison_id foreign key (spatial_comparison_id) references spatial_comparison (id) on delete restrict on update restrict;


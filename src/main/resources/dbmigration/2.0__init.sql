-- apply changes
create table comparsion_session (
  session_id                    uuid not null,
  delete_flag                   boolean default false not null,
  created                       timestamptz not null,
  constraint pk_comparsion_session primary key (session_id)
);

create table concept (
  id                            bigserial not null,
  name                          varchar(255),
  constraint pk_concept primary key (id)
);

create table concept_comparison (
  id                            bigserial not null,
  concepta                      bigint not null,
  conceptb                      bigint not null,
  session_session_id            uuid not null,
  qst_nr                        integer not null,
  rating                        integer not null,
  duration                      bigint not null,
  created                       timestamptz not null,
  modified                      timestamptz not null,
  constraint pk_concept_comparison primary key (id)
);

create table connections (
  id                            bigserial not null,
  sum                           integer not null,
  weight                        float not null,
  target_id                     bigint not null,
  source_id                     bigint not null,
  constraint pk_connections primary key (id)
);

create table settings (
  id                            bigserial not null,
  max_comps                     integer not null,
  max_spats                     integer not null,
  concepts_per_spat             integer not null,
  status_comp                   varchar(255),
  status_spat                   varchar(255),
  index                         integer,
  constraint pk_settings primary key (id)
);

create table spatial_comparison (
  id                            bigserial not null,
  session_session_id            uuid not null,
  duration                      bigint not null,
  konva_result                  json not null,
  scale                         float not null,
  dim_x                         integer not null,
  dim_y                         integer not null,
  created                       timestamptz not null,
  modified                      timestamptz not null,
  constraint pk_spatial_comparison primary key (id)
);

create table spatial_comparison_concept (
  spatial_comparison_id         bigint not null,
  concept_id                    bigint not null,
  constraint pk_spatial_comparison_concept primary key (spatial_comparison_id,concept_id)
);

create table spatial_comparison_clicks_per_concept (
  spatial_comparison_id         bigint not null,
  value                         integer not null
);

create table spatial_node_tracked (
  id                            bigserial not null,
  spatial_comparison_id         bigint not null,
  name                          varchar(255) not null,
  x                             float not null,
  y                             float not null,
  drag_start                    bigint,
  drag_stop                     bigint,
  old_x                         float,
  old_y                         float,
  constraint pk_spatial_node_tracked primary key (id)
);

create table spatial_pos (
  id                            bigserial not null,
  spatial_comparison_id         bigint not null,
  x                             float not null,
  y                             float not null,
  constraint pk_spatial_pos primary key (id)
);

create table spatial_session (
  session_id                    uuid not null,
  delete_flag                   boolean default false not null,
  created                       timestamptz not null,
  constraint pk_spatial_session primary key (session_id)
);

create index ix_concept_comparison_concepta on concept_comparison (concepta);
alter table concept_comparison add constraint fk_concept_comparison_concepta foreign key (concepta) references concept (id) on delete restrict on update restrict;

create index ix_concept_comparison_conceptb on concept_comparison (conceptb);
alter table concept_comparison add constraint fk_concept_comparison_conceptb foreign key (conceptb) references concept (id) on delete restrict on update restrict;

create index ix_concept_comparison_session_session_id on concept_comparison (session_session_id);
alter table concept_comparison add constraint fk_concept_comparison_session_session_id foreign key (session_session_id) references comparsion_session (session_id) on delete restrict on update restrict;

create index ix_connections_target_id on connections (target_id);
alter table connections add constraint fk_connections_target_id foreign key (target_id) references concept (id) on delete restrict on update restrict;

create index ix_connections_source_id on connections (source_id);
alter table connections add constraint fk_connections_source_id foreign key (source_id) references concept (id) on delete restrict on update restrict;

create index ix_spatial_comparison_session_session_id on spatial_comparison (session_session_id);
alter table spatial_comparison add constraint fk_spatial_comparison_session_session_id foreign key (session_session_id) references spatial_session (session_id) on delete restrict on update restrict;

create index ix_spatial_comparison_concept_spatial_comparison on spatial_comparison_concept (spatial_comparison_id);
alter table spatial_comparison_concept add constraint fk_spatial_comparison_concept_spatial_comparison foreign key (spatial_comparison_id) references spatial_comparison (id) on delete restrict on update restrict;

create index ix_spatial_comparison_concept_concept on spatial_comparison_concept (concept_id);
alter table spatial_comparison_concept add constraint fk_spatial_comparison_concept_concept foreign key (concept_id) references concept (id) on delete restrict on update restrict;

create index ix_spatial_comparison_clicks_per_concept_spatial_comparis_1 on spatial_comparison_clicks_per_concept (spatial_comparison_id);
alter table spatial_comparison_clicks_per_concept add constraint fk_spatial_comparison_clicks_per_concept_spatial_comparis_1 foreign key (spatial_comparison_id) references spatial_comparison (id) on delete restrict on update restrict;

create index ix_spatial_node_tracked_spatial_comparison_id on spatial_node_tracked (spatial_comparison_id);
alter table spatial_node_tracked add constraint fk_spatial_node_tracked_spatial_comparison_id foreign key (spatial_comparison_id) references spatial_comparison (id) on delete restrict on update restrict;

create index ix_spatial_pos_spatial_comparison_id on spatial_pos (spatial_comparison_id);
alter table spatial_pos add constraint fk_spatial_pos_spatial_comparison_id foreign key (spatial_comparison_id) references spatial_comparison (id) on delete restrict on update restrict;


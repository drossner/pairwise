-- apply changes
create table connections (
  id                            bigserial not null,
  sum                           integer not null,
  weight                        float not null,
  target_id                     bigint not null,
  source_id                     bigint not null,
  constraint pk_connections primary key (id)
);

create index ix_connections_target_id on connections (target_id);
alter table connections add constraint fk_connections_target_id foreign key (target_id) references concept (id) on delete restrict on update restrict;

create index ix_connections_source_id on connections (source_id);
alter table connections add constraint fk_connections_source_id foreign key (source_id) references concept (id) on delete restrict on update restrict;


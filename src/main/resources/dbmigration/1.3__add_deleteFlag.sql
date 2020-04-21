-- apply changes
alter table comparsion_session add column delete_flag boolean default false not null;

alter table spatial_session add column delete_flag boolean default false not null;


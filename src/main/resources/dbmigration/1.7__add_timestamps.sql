-- apply changes
alter table spatial_node_tracked add column drag_start bigint;
alter table spatial_node_tracked add column drag_stop bigint;


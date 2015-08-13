CREATE TABLE services (
	id serial not null primary key,
	service_uid varchar(100) not null unique,
        current_upload_rate double precision NOT NULL DEFAULT 1.0,
	name varchar(100) not null,
	reportResponseTimeLimit bigint not null -- em ms
);

INSERT INTO services (service_uid,name,reportResponseTimeLimit) 
VALUES ('d428f2f7-ae09-4ecf-b57a-b3eaf2362d38','Backend Server',500);

CREATE TABLE applications (
	id serial not null primary key,
	application_uid varchar(100) not null unique,
	password varchar(100) not null,
        has_subscribed_upload_rate boolean NOT NULL DEFAULT false,
	expiration_date bigint not null default 0,
	service_id integer not null,
	foreign key (service_id) references services (id)
);

CREATE TABLE reports (
	id bigserial not null primary key,
	response_time bigint not null,
	application_uid varchar(100) not null,
	content text not null,
        saved_time time with time zone NOT NULL DEFAULT now(),
	user_id integer default null
);

CREATE TABLE setups (
	id serial not null primary key,
	name varchar(100) not null,
	label varchar(100) not null,
	default_value varchar(100) not null
);

INSERT INTO setups (name,label,default_value) 
VALUES ('Modelo de CPU','cpuModel','unknown'),
('Quantidade de núcleos de processamento','cpuCore','0'),
('Clock do CPU','cpuClock','0.0'),
('Modelo de Dispositivo','deviceModel','unknown'),
('Sistema operacional nativo','nativeOS','unknown'),
('Espaço disponível para armazenamento','storage','0.0'),
('Capacidade de armazenamento da bateria em mAh','battery','0.0'),
('Quantidade de memória disponível','memory','0.0');

CREATE TABLE device_setups (
	setup_id integer not null,
	application_id integer not null,
	content varchar(100) not null,
	foreign key (application_id) references applications (id),
	foreign key (setup_id) references setups (id)
);

CREATE TABLE input_communication_interfaces (
	id serial not null primary key,
	name varchar(100) not null
);

INSERT INTO input_communication_interfaces (name) VALUES ('Socket'),('GCM');

CREATE TABLE input_communication_interface_parameters (
	id serial not null primary key,
	label varchar(100) not null,
	input_communication_interface_id integer not null,
	foreign key (input_communication_interface_id) references input_communication_interfaces (id)
);

INSERT INTO input_communication_interface_parameters (label,input_communication_interface_id) 
VALUES ('ipv4Address',1),('port',1),('deviceKey',2);

CREATE TABLE device_input_communication_parameter_content (
	parameter_id integer not null,
	application_id integer not null,
	content varchar(100) not null,
	foreign key (application_id) references applications (id),
	foreign key (parameter_id) references input_communication_interface_parameters (id),
	primary key (application_id,parameter_id)
);

CREATE TABLE application_upload_rate (
        id serial not null primary key,
        application_id integer not null,
        upload_rate double precision not null default 1.0,
        sent boolean not null default false,
        foreign key (application_id) references applications (id)
);

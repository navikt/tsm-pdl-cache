create table person(
    aktor_id VARCHAR PRIMARY KEY,
    fodselsdato Date,
    navn JSONB
);

create table identer(
    ident VARCHAR PRIMARY KEY,
    gruppe VARCHAR not null,
    historisk bool not null,
    aktor_id VARCHAR REFERENCES person(aktor_id) ON DELETE CASCADE
);

create table books (
    id uuid primary key,
    title varchar(255) not null,
    description text not null default '',
    author varchar(255) not null,
    image_url varchar(300) not null default '',
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
create index idx_books_active_title_id on books (active, title, id);
create index idx_books_active_author on books (active, author);

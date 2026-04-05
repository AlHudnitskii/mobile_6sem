-- ================================================================
-- Battleship Online — Supabase Migration (Run in SQL Editor)
-- ================================================================

-- 1. profiles table
create table if not exists public.profiles (
  id            uuid primary key references auth.users(id) on delete cascade,
  email         text          not null default '',
  nickname      text          not null default 'Player',
  avatar_index  int           not null default 0,
  avatar_url    text          not null default '',
  total_games   int           not null default 0,
  wins          int           not null default 0,
  losses        int           not null default 0,
  created_at    timestamptz   not null default now()
);

-- 2. games table  (boards stored as integer arrays)
create table if not exists public.games (
  id                varchar(6)  primary key,
  player1_id        uuid        references auth.users(id),
  player2_id        uuid        references auth.users(id),
  player1_nickname  text        not null default '',
  player2_nickname  text        not null default '',
  status            text        not null default 'WAITING_FOR_PLAYER',
  player1_board     integer[]   not null default array_fill(0, array[100]),
  player2_board     integer[]   not null default array_fill(0, array[100]),
  player1_ships     text        not null default '[]',
  player2_ships     text        not null default '[]',
  player1_ready     boolean     not null default false,
  player2_ready     boolean     not null default false,
  winner_id         uuid        references auth.users(id),
  created_at        timestamptz not null default now()
);

-- 3. game_records table
create table if not exists public.game_records (
  id                 uuid        primary key default gen_random_uuid(),
  user_id            uuid        references auth.users(id) on delete cascade,
  game_id            varchar(6),
  opponent_nickname  text        not null default '',
  result             text        not null,
  created_at         timestamptz not null default now()
);

-- 4. Enable RLS
alter table public.profiles     enable row level security;
alter table public.games        enable row level security;
alter table public.game_records enable row level security;

-- 5. profiles policies
drop policy if exists "read_profiles"   on public.profiles;
drop policy if exists "insert_profile"  on public.profiles;
drop policy if exists "update_profile"  on public.profiles;

create policy "read_profiles"   on public.profiles for select using (auth.role() = 'authenticated');
create policy "insert_profile"  on public.profiles for insert with check (auth.uid() = id);
create policy "update_profile"  on public.profiles for update using (auth.uid() = id);

-- 6. games policies
drop policy if exists "read_games"   on public.games;
drop policy if exists "insert_game"  on public.games;
drop policy if exists "update_game"  on public.games;

create policy "read_games"   on public.games for select using (auth.role() = 'authenticated');
create policy "insert_game"  on public.games for insert with check (auth.uid() = player1_id);
create policy "update_game"  on public.games for update using (
    auth.uid() = player1_id or
    auth.uid() = player2_id or
    player2_id is null
);

-- 7. game_records policies
drop policy if exists "read_records"   on public.game_records;
drop policy if exists "insert_records" on public.game_records;

create policy "read_records"   on public.game_records for select using (auth.uid() = user_id);
create policy "insert_records" on public.game_records for insert with check (auth.role() = 'authenticated');

-- 8. Enable Realtime for games table
-- IMPORTANT: Also go to Database → Replication and toggle ON for the games table
alter publication supabase_realtime add table public.games;

-- 9. Storage bucket for avatars (run separately if needed)
insert into storage.buckets (id, name, public)
  values ('avatars', 'avatars', true)
  on conflict (id) do nothing;

drop policy if exists "avatar_upload"  on storage.objects;
drop policy if exists "avatar_read"    on storage.objects;
drop policy if exists "avatar_update"  on storage.objects;

create policy "avatar_upload"  on storage.objects for insert
  with check (bucket_id = 'avatars' and auth.role() = 'authenticated');
create policy "avatar_read"    on storage.objects for select
  using (bucket_id = 'avatars');
create policy "avatar_update"  on storage.objects for update
  using (bucket_id = 'avatars' and auth.role() = 'authenticated');

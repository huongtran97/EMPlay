# EMPlay – Figma Component Inventory

Fonts: **Inter** (body/UI) · **Playfair Display Medium** (display titles)  
Themes: Light (Parchment) · Dark (Cinema Navy)

---

## 1. Tokens (already in `tokens.json`)
Import via **Tokens Studio for Figma** → Sync → Local file → select `tokens.json`.

---

## 2. Cards

### PosterCard
`layout/movie_item.xml`, `layout/tvshow_popular_item.xml`
- Size: **107 × 160 dp** · corner radius `radius_s` (8)
- Full-bleed image, `centerCrop`
- Optional top-right badge (type label: "TV", "Movie") — pill background, `text_badge` 11sp bold
- Stroke: `poster_card_stroke` 1dp

### TrendingTicketCard  *(hero banner item)*
`layout/item_trending_banner.xml`
- Size: full-width × **210 dp** · ticket-stub shape (notched left/right edges, dashed tear-line)
- **Left section**: poster thumbnail 143 dp tall (`radius_s`) · Playfair title 2 lines · meta caption (genre · cert)
- **Right stub** (84 dp wide): "ADMIT ONE" eyebrow · large rating number in `accent` · Trailer button (accent fill, 48 dp tall)

### FilmstripCard
`layout/item_filmstrip_movie.xml`
- Horizontal thumbnail + title + meta in a row

### WhatsNewCard
`layout/item_whats_new_movie.xml`, `item_whats_new_tvshow.xml`
- Backdrop image with overlay · title · release date pill

### RecentlyAddedCard
`layout/item_recently_added.xml`
- Compact poster with title below

### WatchlistPoster
`layout/item_watchlist_poster.xml`, `item_recently_saved_poster.xml`
- Portrait poster, no text overlay

### FilmographyRow
`layout/item_filmography_row.xml`
- Thumbnail left + title / year / role right

### MyListRow
`layout/item_mylist_row.xml`
- Poster thumbnail + title + year + swipe-to-delete affordance

### EpisodeItem
`layout/episode_item.xml`
- Still thumbnail · episode number + title · runtime · overview excerpt

### OnAirCard
`layout/item_onair_more.xml`, `item_on_air_seeall.xml`
- Show poster + airing badge (green `airing` color)

### SearchMediaCard
`layout/item_search_media.xml`
- Horizontal: poster thumbnail · title · year · type badge · rating

### SearchPersonCard
`layout/item_search_person.xml`
- Circle avatar · name · known-for text

### ProviderLogo
`layout/item_provider.xml`
- Square logo tile (streaming provider icon)

### OriginCard
`layout/item_origin_card.xml`
- Country flag / label chip for filter browsing

---

## 3. Chips & Badges

### GenreChip
`layout/genres_item.xml` — style `GenreChipStyle`
- Background: `bg_chip_cinematic`, stroke `border` hairline, corner `radius_pill`
- Text: `text_2` 11sp Inter · per-genre accent color (see token `global.color.genre.*`)

### FilterChip (Material)
`layout/fragment_filter_bottom_sheet.xml` — style `ChipStyle`
- Transparent bg, stroke `border` 0.5dp, pill corners, 32 dp min height

### MetaChip (detail screen)
style `Widget.EMPlay.Chip`
- Transparent bg + border · `text_2` 11sp · pad 10/3 dp

### AccentChip (selected / rating)
style `Widget.EMPlay.ChipAccent`
- Bg: `accent` fill · `on_accent` text

### TypeBadge
- Pill bg · 11sp bold · top-right on poster cards

### AiringBadge
- Background `badge_airing_muted` · text `airing` · tiny pill

### RatingBadge
- IMDB yellow or TMDB blue pill · rating number

### RankBadge (poster overlay)
- Circle `bg_rank_tint` · number in `text_1`

---

## 4. Navigation

### BottomNav
`layout/bottom_nav_view.xml`, `component_bottom_nav.xml`
- 3 tabs: Home · Search · Profile
- Pill indicator fill `nav_indicator_fill` · icon size 20 dp
- Active: `nav.icon_active` (accent) · Inactive: `nav.icon_inactive` (text_3)
- Background `bg_surface` with pill backdrop `nav_pill_bg`

### SearchPill
`layout/search_pill_item.xml`
- Rounded search bar: `radius_m`, stroke `border`, hint text `text_3`

---

## 5. Screens / Fragments

| Screen | Layout file |
|--------|-------------|
| Home | `home_view.xml` |
| Search | `search_view.xml` |
| Watchlist | `watchlist_view.xml` + `fragment_mylist.xml` |
| Movie Detail | `activity_detail_movie.xml` |
| TV Detail | `activity_detail_tv.xml` |
| Cast Detail | `activity_cast.xml` |
| Season Detail | `season_details_view.xml` |
| Genre Results | `activity_genre_results.xml` |
| Trending / See All | `trending_see_all_view.xml` |
| On Air | `activity_see_all.xml` |
| TV by Genre | `tv_by_genre_view.xml` |
| Whats New | `whats_new_view.xml` |
| WTW Released | `wtw_released_view.xml` |
| WTW Unreleased | `wtw_unreleased_view.xml` |
| Splash | `activity_splash.xml` |
| Login | `activity_login.xml` |
| Profile | `customer_profile_view.xml` |
| About | `activity_about.xml` |

---

## 6. Dialogs & Bottom Sheets

| Component | Layout file |
|-----------|-------------|
| Welcome / guest dialog | `dialog_welcome.xml` |
| Forgot password | `dialog_forgot_password.xml` |
| Filter bottom sheet | `fragment_filter_bottom_sheet.xml` |
| Trailer bottom sheet | `fragment_trailer_bottom_sheet.xml` |
| Privacy policy dialog | `dialog_privacy_policy.xml` |
| Open source libraries | `dialog_libraries.xml` |

---

## 7. Shimmer Skeletons

Three shimmer placeholders to build as loading states:
- `shimmer_item_filmstrip.xml` — horizontal filmstrip item
- `shimmer_item_origin.xml` — origin country card
- `shimmer_item_poster.xml` — portrait poster card

---

## 8. Typography Scale (Inter, body weight 400)

| Name | Size | Usage |
|------|------|-------|
| Micro | 8sp | Eyebrow labels, "ADMIT ONE" |
| Caption | 10sp | Meta beneath titles |
| Badge | 11sp | Chip text, badges |
| Meta | 13sp | Secondary info rows |
| Body | 16sp | Overview text |
| Section Header | 18sp | Section titles |
| Title | 22sp | Card titles |
| Display S | 25sp | Hero title (Playfair) |
| Display L | 32sp | Rating number stub (Playfair) |

---

## 9. Recommended Figma Setup Steps

1. **Install Tokens Studio for Figma** (free plugin)
2. Sync → Local file → point to `tokens.json`
3. In the Themes dropdown activate **Light** or **Dark**
4. Create a new page per section above (Tokens, Cards, Chips, Nav, Screens)
5. Add Inter + Playfair Display via Google Fonts in Figma
6. Build each card as an **Auto Layout** frame using the spacing/radius tokens
7. Use **Variants** for cards that have multiple states (default / selected / loading shimmer)
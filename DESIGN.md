---
name: Kinetic Sound
colors:
  surface: '#0f1509'
  surface-dim: '#0f1509'
  surface-bright: '#343b2d'
  surface-container-lowest: '#091005'
  surface-container-low: '#171e11'
  surface-container: '#1b2215'
  surface-container-high: '#252c1f'
  surface-container-highest: '#303729'
  on-surface: '#e1e3d9'
  on-surface-variant: '#c2c9b7'
  outline: '#8c9383'
  outline-variant: '#42493c'
  primary: '#6ee304'
  on-primary: '#083900'
  primary-container: '#115300'
  on-primary-container: '#89ff4c'
  secondary: '#bccbb0'
  on-secondary: '#273421'
  secondary-container: '#3d4b36'
  on-secondary-container: '#d8e7cb'
  tertiary: '#a0cfd2'
  on-tertiary: '#00373a'
  tertiary-container: '#1e4e51'
  on-tertiary-container: '#bbecef'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
typography:
  font-family: Sora, sans-serif
  headings:
    weight: 700
    line-height: 1.2
  body:
    weight: 400
    line-height: 1.5
spacing:
  base: 4px
  scale: [0, 4, 8, 12, 16, 24, 32, 48, 64]
roundness:
  radius: 4px
---

# Kinetic Sound Design System

## Overview
Kinetic Sound is a high-contrast, "stage-ready" design system built for professional musicians and stage crews. It utilizes a deep midnight-green base with vibrant electric lime accents to ensure maximum legibility and visual impact in low-light environments typical of backstage areas and music venues.

## Visual Personality
- **Dark & Immersive**: The dark color palette reduces eye strain and fits the aesthetic of the music industry.
- **Electric Accents**: Use of `#6ee304` (Electric Lime) provides clear calls to action and status indicators.
- **Modern Geometry**: Clean lines, subtle rounding (4px), and bold typography create a precise, technical feel.

## Color Palette
The system uses semantic tokens to maintain consistency:
- **Surface**: The foundation of the app, using deep, dark greens (`#0f1509`).
- **Primary**: Electric Lime (`#6ee304`) for high-priority actions and active states.
- **Secondary/Tertiary**: Subdued greens and teals for supporting information and grouped content.

## Typography
- **Primary Typeface**: **Sora**
- A modern sans-serif with a geometric touch, chosen for its clarity at small sizes on mobile devices and its bold, impactful presence in headings.

## Components
The application utilizes a consistent set of shared components:
- **Top Bar**: Minimalist navigation with profile access and branding.
- **Bottom Navigation**: Persistent 5-tab bar for core app navigation (Home, Calendar, Requests, Lists, Venues).
- **Cards**: High-contrast containers for gig details, band profiles, and requests.
- **Buttons**: Full-width primary buttons using the electric lime accent.

## Application Architecture
The project encompasses several key flows:
1. **Authentication**: Secure entry via the Login screen.
2. **Operations Dashboard**: Real-time overview of schedules and requests.
3. **Logistics Management**: Band Setup and Venue Setup for technical and team organization.
4. **Scheduling**: A specialized Calendar view for managing blocked dates and tour dates.
5. **Networking**: A Directory for discovery of other bands, profiles, and venues.

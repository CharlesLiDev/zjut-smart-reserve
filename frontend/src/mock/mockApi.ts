import { fetchMockJson } from './mockClient';

export type Venue = {
  id: number;
  name: string;
  type: string;
  capacity: number;
  location: string;
  equipment: string[];
  status: string;
  image?: string;
  tags?: string[];
  area?: number;
  floor?: number;
  facilities?: string[];
};

export type Notice = {
  id: number | string;
  type: string;
  time: string;
  title: string;
  content: string;
};

export type Appointment = {
  eventName: string;
  location: string;
  date: string;
  timeSlot: string;
  status: string;
};

export type OccupiedSlot = {
  id: number | string;
  start: number;
  end: number;
};

export type SidebarConfig = {
  user: {
    avatarText: string;
    name: string;
    role: string;
  };
  menuItems: Array<{
    name: string;
    path: string;
    icon: string;
  }>;
};

type DateSlotMap = Record<string, OccupiedSlot[]>;

type VenueScheduleMap = Record<
  string,
  {
    default: OccupiedSlot[];
    dates?: DateSlotMap;
  }
>;

export async function listVenues(): Promise<Venue[]> {
  return fetchMockJson<Venue[]>('mock/venues.json');
}

export async function getVenueById(id: number): Promise<Venue | null> {
  const venues = await listVenues();
  return venues.find((v) => v.id === id) ?? null;
}

export async function listNotices(): Promise<Notice[]> {
  return fetchMockJson<Notice[]>('mock/notices.json');
}

export async function listAppointments(): Promise<Appointment[]> {
  return fetchMockJson<Appointment[]>('mock/appointments.json');
}

export async function getOccupiedSlotsByDate(date: string): Promise<OccupiedSlot[]> {
  const map = await fetchMockJson<DateSlotMap>('mock/occupied-slots.json');
  return map[date] ?? map['default'] ?? [];
}

export async function getVenueOccupiedSlots(venueId: number, date: string): Promise<OccupiedSlot[]> {
  const schedules = await fetchMockJson<VenueScheduleMap>('mock/venue-schedules.json');
  const venue = schedules[String(venueId)];
  if (!venue) return [];
  return venue.dates?.[date] ?? venue.default ?? [];
}

export async function getSidebarConfig(): Promise<SidebarConfig> {
  return fetchMockJson<SidebarConfig>('mock/sidebar.json');
}


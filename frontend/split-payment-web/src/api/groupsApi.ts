
import { apiClient } from "@/api/apiClient";

export interface GroupMember {
  userId: string;
  displayName: string;
  joinedAt: string;
}

export interface Group {
  id: string;
  name: string;
  createdBy: string;
  createdAt: string;
  members: GroupMember[];
}

export interface CreateGroupRequest {
  name: string;
}

export function getMyGroups(): Promise<Group[]> {
  return apiClient<Group[]>("/groups");
}

export function createGroup(
  request: CreateGroupRequest
): Promise<Group> {
  return apiClient<Group>("/groups", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function getGroupDetails(groupId: string): Promise<Group> {
  return apiClient<Group>(`/groups/${groupId}`);
}
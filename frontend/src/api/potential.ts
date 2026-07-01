import api, { type ApiResponse } from '@/api/instance'

export type RelationshipCode = '01' | '02'
export type Gender = 'MALE' | 'FEMALE'

export interface ParentCustomerSearchRequest {
    name: string
    birthDate: string
    relationshipCode: RelationshipCode
    phone: string
    rrn: string
    address: string
}

export interface ParentCustomerSearchResponse {
    integratedCustomerId: number
    name: string
    birthDate: string
    relationshipCode: RelationshipCode
    relationshipName: string
    phone: string
    address: string
}

export interface PotentialCustomerCreateRequest {
    parentCustomerId: number
    relationshipCode: RelationshipCode
    name: string
    gender: Gender
    birthDate: string
}

export interface PotentialCustomerCreateResponse {
    potentialCustomerId: number
    parentCustomerId: number
    relationshipCode: RelationshipCode
    relationshipName: string
    name: string
    gender: Gender
    birthDate: string
    consultStatusCode: string
    consultStatusName: string
    conversionStatusCode: string
    conversionStatusName: string
    createdAt: string
}

export interface PotentialCustomerDetailResponse {
    potentialCustomerId: number
    parentCustomerId: number
    parentName: string
    parentBirthDate: string
    parentPhone: string
    parentAddress: string
    relationshipCode: RelationshipCode
    relationshipName: string
    name: string
    gender: Gender
    birthDate: string
    age: number
    consultStatusCode: string
    consultStatusName: string
    conversionStatusCode: string
    conversionStatusName: string
}

export interface PotentialCustomerUpdateRequest {
    name: string
    gender: Gender
    birthDate: string
}

export interface PotentialCustomerDeleteResponse {
    potentialCustomerId: number
    deletedAt: string
}

export interface PotentialCustomerListItem {
    potentialCustomerId: number
    customerName: string
    gender: Gender
    age: number
    birthDate: string
    guardianName: string
    guardianRelationshipCode: RelationshipCode
    guardianRelationshipName: string
    guardianAge: number
    guardianPhone: string
    isActive: boolean
    createdAt: string
}

export async function getPotentialCustomers() {
    const response = await api.get<ApiResponse<PotentialCustomerListItem[]>>(
        '/v1/potential-customers',
    )

    return response.data.data
}

export async function getPotentialCustomerDetail(potentialCustomerId: number) {
    const response = await api.get<ApiResponse<PotentialCustomerDetailResponse>>(
        `/v1/potential-customers/${potentialCustomerId}`,
    )

    return response.data.data
}

export async function getParentCustomers() {
    const response = await api.get<ApiResponse<ParentCustomerSearchResponse[]>>(
        '/v1/potential-customers/parents',
    )

    return response.data.data    
}

export async function searchParentCustomer(data: ParentCustomerSearchRequest) {
    const response = await api.post<ApiResponse<ParentCustomerSearchResponse>>(
        '/v1/potential-customers/parent/search',
        data,
    )

    return response.data.data
}

export async function createPotentialCustomer(data: PotentialCustomerCreateRequest) {
    const response = await api.post<ApiResponse<PotentialCustomerCreateResponse>>(
        '/v1/potential-customers',
        data,
    )

    return response.data.data
}

export async function updatePotentialCustomer(
    potentialCustomerId: number,
    data: PotentialCustomerUpdateRequest,
) {
    const response = await api.patch<ApiResponse<PotentialCustomerDetailResponse>>(
        `/v1/potential-customers/${potentialCustomerId}`,
        data,
    )

    return response.data.data
}

export async function deletePotentialCustomer(potentialCustomerId: number) {
    const response = await api.delete<ApiResponse<PotentialCustomerDeleteResponse>>(
        `/v1/potential-customers/${potentialCustomerId}`,
    )

    return response.data.data
}
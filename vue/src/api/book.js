// src/api/book.js
import request from '@/utils/request'

export function getBookStructure(workId) {
    return request({
        url: `/api/books/${workId}/structure`,
        method: 'get'
    })
}

export function createVolume(data) {
    return request({
        url: '/api/books/volumes',
        method: 'post',
        data
    })
}

export function createChapter(data) {
    return request({
        url: '/api/books/chapters',
        method: 'post',
        data
    })
}

export function updateChapter(data) {
    return request({
        url: '/api/books/chapters',
        method: 'put',
        data
    })
}

export function deleteVolume(id) {
    return request({
        url: `/api/books/volumes/${id}`,
        method: 'delete'
    })
}

export function deleteChapter(id) {
    return request({
        url: `/api/books/chapters/${id}`,
        method: 'delete'
    })
}

export function updateVolume(data) {
    return request({
        url: '/api/books/volumes',
        method: 'put',
        data
    })
}
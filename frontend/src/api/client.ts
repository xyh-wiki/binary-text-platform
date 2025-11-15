// src/api/client.ts
export const API_BASE =
    import.meta.env.VITE_API_BASE_URL || window.location.origin

export async function extractSingle(formData: FormData) {
    const resp = await fetch(`${API_BASE}/api/extract/single`, {
        method: 'POST',
        body: formData,
    })

    if (!resp.ok) {
        // 这里可以根据需要细化错误信息
        throw new Error(`HTTP ${resp.status}`)
    }

    return resp.json()
}
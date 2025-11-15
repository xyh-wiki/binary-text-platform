import React, { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'

type ExtractResult = {
    fileName: string
    fileSize: number
    fileType: string | null
    content: string | null
    errorMsg: string | null
}

type FileTask = {
    id: string
    file: File
    status: 'pending' | 'uploading' | 'success' | 'error'
    result?: ExtractResult
}

// 后端 API 基础地址，从构建环境变量中注入
const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

/**
 * Author:XYH
 * Date:2025-11-15
 * Description: 前端主界面组件，负责文件上传、进度展示、多语言切换以及与后端 /api/extract 接口交互。
 *              默认语言为英文，可在页面右上角切换到中文。
 */
const App = () => {
    // ✅ 修正：原来是 const {{ t, i18n }} = useTranslation()
    const { t, i18n } = useTranslation()
    const [tasks, setTasks] = useState<FileTask[]>([])
    const [concurrency] = useState<number>(2)
    const [running, setRunning] = useState(false)

    /**
     * 处理语言切换。
     *
     * @param lng 目标语言标识，如 'en' 或 'zh'
     */
    const changeLanguage = (lng: 'en' | 'zh') => {
        i18n.changeLanguage(lng)
    }

    /**
     * 将用户选择的文件列表加入到任务队列中。
     *
     * @param event 文件选择事件对象
     */
    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const files = event.target.files
        if (!files || files.length === 0) return

        const newTasks: FileTask[] = []
        for (let i = 0; i < files.length; i++) {
            const file = files.item(i)!
            newTasks.push({
                id: `${Date.now()}-${i}-${file.name}`,
                file,
                status: 'pending',
            })
        }
        setTasks(prev => [...prev, ...newTasks])

        // 清空 input，否则同一个文件下次无法触发 change 事件
        event.target.value = ''
    }

    /**
     * 启动批量上传和解析流程，前端做简易并发控制，以减少对后端的压力。
     */
    const startProcess = useCallback(() => {
        if (running) return
        setRunning(true)

        const run = async () => {
            let tasksRef: FileTask[] = [...tasks]

            // 从任务列表中取出下一个 pending 任务
            const pickNextPending = (): FileTask | undefined => {
                return tasksRef.find(t => t.status === 'pending')
            }

            // 更新某个任务的状态
            const updateTask = (id: string, partial: Partial<FileTask>) => {
                tasksRef = tasksRef.map(t => (t.id === id ? { ...t, ...partial } : t))
                setTasks(tasksRef)
            }

            // 单个前端“worker”，串行消费 pending 任务
            const worker = async () => {
                while (true) {
                    const next = pickNextPending()
                    if (!next) break

                    updateTask(next.id, { status: 'uploading' })
                    try {
                        const formData = new FormData()
                        formData.append('file', next.file)
                        const resp = await fetch(`${API_BASE}/api/extract/single`, {
                            method: 'POST',
                            body: formData,
                        })
                        const data: ExtractResult = await resp.json()
                        updateTask(next.id, { status: 'success', result: data })
                    } catch (e) {
                        console.error(e)
                        updateTask(next.id, { status: 'error' })
                    }
                }
            }

            const workers: Promise<void>[] = []
            for (let i = 0; i < concurrency; i++) {
                workers.push(worker())
            }

            await Promise.all(workers)
            setRunning(false)
        }

        run()
    }, [running, concurrency, tasks])

    /**
     * 将提取结果复制到剪贴板。
     *
     * @param task 文件任务对象
     */
    const copyContent = async (task: FileTask) => {
        if (!task.result || !task.result.content) {
            alert(t('downloadFail'))
            return
        }
        try {
            await navigator.clipboard.writeText(task.result.content)
            alert(t('copySuccess'))
        } catch (e) {
            console.error(e)
            alert(t('copyFail'))
        }
    }

    /**
     * 将提取结果下载为本地 .txt 文件。
     *
     * @param task 文件任务对象
     */
    const downloadContent = (task: FileTask) => {
        if (!task.result || !task.result.content) {
            alert(t('downloadFail'))
            return
        }
        const blob = new Blob([task.result.content], { type: 'text/plain;charset=utf-8' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = (task.result.fileName || 'extract') + '.txt'
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
    }

    /**
     * 将文件大小转为更友好的显示格式。
     *
     * @param size 文件大小（字节数）
     * @returns 格式化后字符串
     */
    const formatSize = (size: number): string => {
        if (size < 1024) return `${size} B`
        const kb = size / 1024
        if (kb < 1024) return `${kb.toFixed(2)} KB`
        const mb = kb / 1024
        return `${mb.toFixed(2)} MB`
    }

    return (

        // 外层
        <div
            style={{
                minHeight: '100vh',
                margin: 0,
                fontFamily:
                    '-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Arial,sans-serif',
                background: '#0f172a',
                color: '#e5e7eb',
                padding: '40px 16px',
                width: '100%',
            }}
        >

            <div
                style={{
                    width: '100%',
                    maxWidth: '100%',
                    background: '#020617',
                    borderRadius: 24,
                    boxShadow: '0 24px 60px rgba(15,23,42,0.8)',
                    padding: 24,
                    border: '1px solid rgba(148,163,184,0.25)',
                }}
            >
                {/* 顶部标题 + 语言切换 */}
                <header
                    style={{
                        marginBottom: 24,
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'flex-start',
                        gap: 12,
                        flexWrap: 'wrap',
                    }}
                >
                    <div>
                        <h1 style={{ fontSize: 26, margin: 0, fontWeight: 700 }}>{t('title')}</h1>
                        <p style={{ marginTop: 8, color: '#9ca3af', fontSize: 13 }}>{t('subtitle')}</p>
                        <p style={{ marginTop: 4, color: '#64748b', fontSize: 11 }}>
                            {t('headerDomainNote')}
                        </p>
                    </div>

                    <div
                        style={{
                            display: 'flex',
                            gap: 8,
                            alignItems: 'center',
                        }}
                    >
                        <button
                            onClick={() => changeLanguage('en')}
                            style={{
                                padding: '4px 10px',
                                borderRadius: 9999,
                                border: i18n.language === 'en' ? '1px solid #38bdf8' : '1px solid #4b5563',
                                background: i18n.language === 'en' ? '#0f172a' : 'transparent',
                                color: '#e5e7eb',
                                fontSize: 12,
                                cursor: 'pointer',
                            }}
                        >
                            {t('langEn')}
                        </button>
                        <button
                            onClick={() => changeLanguage('zh')}
                            style={{
                                padding: '4px 10px',
                                borderRadius: 9999,
                                border: i18n.language === 'zh' ? '1px solid #38bdf8' : '1px solid #4b5563',
                                background: i18n.language === 'zh' ? '#0f172a' : 'transparent',
                                color: '#e5e7eb',
                                fontSize: 12,
                                cursor: 'pointer',
                            }}
                        >
                            {t('langZh')}
                        </button>
                    </div>
                </header>

                {/* 上传区域 */}
                <section
                    style={{
                        borderRadius: 16,
                        border: '1px dashed rgba(148,163,184,0.6)',
                        padding: 20,
                        marginBottom: 24,
                        background:
                            'radial-gradient(circle at top left, rgba(56,189,248,0.15), transparent 55%), radial-gradient(circle at bottom right, rgba(167,139,250,0.25), transparent 60%)',
                    }}
                >
                    <div style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between' }}>
                        <div>
                            <div style={{ fontSize: 16, fontWeight: 600 }}>{t('selectFiles')}</div>
                            <div style={{ fontSize: 12, color: '#e5e7eb', marginTop: 4 }}>
                                {t('selectFilesDesc')}
                            </div>
                        </div>
                        <div style={{ fontSize: 12, color: '#a5b4fc' }}>
                            {t('concurrencyLabel')}: {concurrency}
                        </div>
                    </div>

                    <div
                        style={{
                            display: 'flex',
                            gap: 12,
                            alignItems: 'center',
                            flexWrap: 'wrap',
                        }}
                    >
                        <label
                            style={{
                                padding: '8px 16px',
                                borderRadius: 9999,
                                border: '1px solid rgba(148,163,184,0.7)',
                                fontSize: 14,
                                cursor: 'pointer',
                                background: 'rgba(15,23,42,0.6)',
                            }}
                        >
                            {t('selectFiles')}
                            <input
                                type="file"
                                multiple
                                style={{ display: 'none' }}
                                onChange={handleFileChange}
                            />
                        </label>

                        <button
                            onClick={startProcess}
                            disabled={running || tasks.length === 0}
                            style={{
                                padding: '8px 20px',
                                borderRadius: 9999,
                                border: 'none',
                                fontSize: 14,
                                fontWeight: 500,
                                cursor: running || tasks.length === 0 ? 'not-allowed' : 'pointer',
                                background: running || tasks.length === 0 ? '#4b5563' : '#22c55e',
                                color: '#020617',
                            }}
                        >
                            {running ? t('processing') : t('startExtract')}
                        </button>
                    </div>
                </section>

                {/* 任务列表区域 */}
                <section>
                    <div style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between' }}>
            <span style={{ fontSize: 14, color: '#d1d5db' }}>
              {t('currentTasks')}: {tasks.length}
            </span>
                    </div>

                    <div
                        style={{
                            maxHeight: '60vh',
                            overflowY: 'auto',
                            borderRadius: 16,
                            border: '1px solid rgba(55,65,81,0.9)',
                            padding: 12,
                            background: 'rgba(15,23,42,0.8)',
                        }}
                    >
                        {tasks.length === 0 && (
                            <div
                                style={{
                                    fontSize: 13,
                                    color: '#9ca3af',
                                    textAlign: 'center',
                                    padding: 24,
                                }}
                            >
                                {t('noTasks')}
                            </div>
                        )}

                        {tasks.map(task => (
                            <div
                                key={task.id}
                                style={{
                                    borderRadius: 12,
                                    padding: 12,
                                    marginBottom: 8,
                                    background: 'rgba(15,23,42,0.9)',
                                    border: '1px solid rgba(75,85,99,0.9)',
                                }}
                            >
                                <div
                                    style={{
                                        display: 'flex',
                                        justifyContent: 'space-between',
                                        alignItems: 'center',
                                        marginBottom: 6,
                                    }}
                                >
                                    <div style={{ fontSize: 13, fontWeight: 500 }}>
                                        {task.file.name}
                                        <span style={{ marginLeft: 8, fontSize: 12, color: '#9ca3af' }}>
                      ({formatSize(task.file.size)})
                    </span>
                                    </div>
                                    <div style={{ fontSize: 12 }}>
                                        {task.status === 'pending' && (
                                            <span style={{ color: '#fbbf24' }}>{t('statusPending')}</span>
                                        )}
                                        {task.status === 'uploading' && (
                                            <span style={{ color: '#22c55e' }}>{t('statusUploading')}</span>
                                        )}
                                        {task.status === 'success' && (
                                            <span style={{ color: '#4ade80' }}>{t('statusSuccess')}</span>
                                        )}
                                        {task.status === 'error' && (
                                            <span style={{ color: '#f97373' }}>{t('statusError')}</span>
                                        )}
                                    </div>
                                </div>

                                {task.result && (
                                    <>
                                        <div
                                            style={{
                                                fontSize: 12,
                                                color: '#9ca3af',
                                                marginBottom: 6,
                                                display: 'flex',
                                                gap: 12,
                                                flexWrap: 'wrap',
                                            }}
                                        >
                      <span>
                        {t('fileType')}: {task.result.fileType || 'N/A'}
                      </span>
                                            {task.result.errorMsg && (
                                                <span style={{ color: '#f97373' }}>
                          {t('error')}: {task.result.errorMsg}
                        </span>
                                            )}
                                        </div>
                                        {task.result.content && (
                                            <div
                                                style={{
                                                    maxHeight: 160,
                                                    overflowY: 'auto',
                                                    padding: 8,
                                                    borderRadius: 8,
                                                    background: '#020617',
                                                    border: '1px solid rgba(31,41,55,0.9)',
                                                    fontSize: 12,
                                                    whiteSpace: 'pre-wrap',
                                                }}
                                            >
                                                {task.result.content.slice(0, 2000)}
                                                {task.result.content.length > 2000 && '...'}
                                            </div>
                                        )}

                                        <div style={{ marginTop: 8, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                                            <button
                                                onClick={() => copyContent(task)}
                                                disabled={!task.result?.content}
                                                style={{
                                                    padding: '4px 10px',
                                                    borderRadius: 9999,
                                                    border: '1px solid rgba(148,163,184,0.8)',
                                                    background: 'transparent',
                                                    color: '#e5e7eb',
                                                    fontSize: 12,
                                                    cursor: task.result?.content ? 'pointer' : 'not-allowed',
                                                }}
                                            >
                                                {t('copyText')}
                                            </button>
                                            <button
                                                onClick={() => downloadContent(task)}
                                                disabled={!task.result?.content}
                                                style={{
                                                    padding: '4px 10px',
                                                    borderRadius: 9999,
                                                    border: 'none',
                                                    background: '#3b82f6',
                                                    color: '#f9fafb',
                                                    fontSize: 12,
                                                    cursor: task.result?.content ? 'pointer' : 'not-allowed',
                                                }}
                                            >
                                                {t('downloadTxt')}
                                            </button>
                                        </div>
                                    </>
                                )}

                                {task.status === 'error' && !task.result && (
                                    <div style={{ fontSize: 12, color: '#f97373', marginTop: 4 }}>
                                        {t('uploadError')}
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                </section>
            </div>
        </div>
    )
}

export default App
package io.horizontalsystems.bankwallet.modules.nft.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.bankwallet.entities.DataState
import io.horizontalsystems.bankwallet.entities.ViewState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NftCollectionsViewModel(private val service: NftCollectionsService) : ViewModel() {
    val priceType by service::priceType

    var viewState by mutableStateOf<ViewState?>(null)
        private set
    var loading by mutableStateOf(false)
        private set

    var collectionViewItems by mutableStateOf<List<NftCollectionViewItem>>(listOf())
        private set

    init {
        viewModelScope.launch {
            service.collectionItems
                .collect {
                    handleNftCollections(it)
                }
        }

        service.start()
    }

    private fun handleNftCollections(nftCollectionsState: DataState<List<NftCollectionItem>>) {
        loading = nftCollectionsState.loading

        nftCollectionsState.dataOrNull?.let {
            viewState = ViewState.Success

            syncItems(it)
        }
    }

    private fun syncItems(collectionItems: List<NftCollectionItem>) {
        val expandedStates = collectionViewItems.map {
            it.slug to it.expanded
        }.toMap()

        collectionViewItems = collectionItems.map { nftCollectionItem ->
            NftCollectionViewItem(
                slug = nftCollectionItem.slug,
                name = nftCollectionItem.name,
                imageUrl = nftCollectionItem.imageUrl,
                assets = nftCollectionItem.assets,
                expanded = expandedStates[nftCollectionItem.slug] ?: false
            )
        }
    }

    override fun onCleared() {
        service.stop()
    }

    fun refresh() {
        viewModelScope.launch {
            loading = true
            service.refresh()
            loading = false
        }
    }

    fun toggleCollection(collection: NftCollectionViewItem) {
        val index = collectionViewItems.indexOf(collection)

        if (index != -1) {
            collectionViewItems = collectionViewItems.toMutableList().apply {
                this[index] = collection.copy(expanded = !collection.expanded)
            }
        }
    }

    fun updatePriceType(priceType: PriceType) {
        service.updatePriceType(priceType)
    }

}
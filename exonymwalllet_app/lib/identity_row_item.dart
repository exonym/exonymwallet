import 'package:exonymwallet/exonymwallet.dart';
import 'package:flutter/cupertino.dart';
import 'package:provider/provider.dart';

import 'model/app_state_model.dart';
import 'styles.dart';

class IdentityRowItem extends StatelessWidget {
  const IdentityRowItem({
    required this.identity,
    super.key,
  });

  final IdentityContainer identity;

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      top: false,
      bottom: false,
      minimum: const EdgeInsets.only(
        left: 0,
        top: 8,
        bottom: 8,
        right: 8,
      ),
      child: CupertinoListTile(
        leading: ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: const Icon(CupertinoIcons.person_alt),
        ),
        leadingSize: 68,
        title: Text(
          identity.username,
          style: Styles.itemHeader,
        ),
        subtitle: Text(
          '${identity.identityType}',
          style: Styles.itemSubtitle,
        ),
        trailing: CupertinoButton(
          padding: EdgeInsets.zero,
          onPressed: () async {
            final model = Provider.of<AppStateModel>(context, listen: false);
            model.setIdentity(identity.username);
            if (await model.isDeviceSetup()){
              debugPrint("Yo");
            }
          },
          child: const Icon(
            CupertinoIcons.hexagon,
            semanticLabel: 'Select ID',
          ),
        ),
      ),
    );
  }

}